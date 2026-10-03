import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import { PrismaClient } from '@prisma/client';
import OpenAI from 'openai';
import { NOVA_SYSTEM_PROMPT } from './prompts';
import { requireAuth } from './auth';
import { CRISIS_RESPONSE, isCrisisMessage } from './crisis';

const app = express();
const prisma = new PrismaClient();
const PORT = process.env.PORT || 3000;
const auth = requireAuth(prisma);

// Plan gratuito: mensajes por día con NOVA (PLAN_MVP.md, punto 12).
const NOVA_DAILY_LIMIT = Number(process.env.NOVA_DAILY_LIMIT) || 10;
// Mensajes previos que se envían a la IA como contexto.
const NOVA_HISTORY_LENGTH = 10;
const MAX_TEXT_LENGTH = 5000;

const openai = process.env.OPENAI_API_KEY ? new OpenAI({ apiKey: process.env.OPENAI_API_KEY }) : null;

app.use(cors());
app.use(express.json());

// Endpoint de prueba (Healthcheck)
app.get('/health', (req, res) => {
  res.json({ status: 'ok', message: 'API de Senda funcionando correctamente' });
});

// --- Endpoints de Diario y Ánimo (Ejemplos) ---

app.post('/api/mood', auth, async (req, res) => {
  try {
    const { score, emotions, notes } = req.body;
    if (!Number.isInteger(score) || score < 1 || score > 5) {
      return res.status(400).json({ error: 'score debe ser un entero entre 1 y 5' });
    }

    const newEntry = await prisma.moodEntry.create({
      data: {
        userId: req.user!.id,
        score,
        emotions: Array.isArray(emotions) ? emotions.map(String) : [],
        notes: typeof notes === 'string' ? notes : null
      }
    });
    res.status(201).json(newEntry);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Error guardando el registro de ánimo' });
  }
});

app.get('/api/mood', auth, async (req, res) => {
  try {
    const entries = await prisma.moodEntry.findMany({
      where: { userId: req.user!.id },
      orderBy: { createdAt: 'desc' }
    });
    res.json(entries);
  } catch (error) {
    res.status(500).json({ error: 'Error consultando los registros' });
  }
});

// --- Diario ---

app.post('/api/journal', auth, async (req, res) => {
  try {
    const { content } = req.body;
    if (typeof content !== 'string' || content.trim() === '') {
      return res.status(400).json({ error: 'content es obligatorio' });
    }
    if (content.length > MAX_TEXT_LENGTH) {
      return res.status(400).json({ error: `content no puede superar ${MAX_TEXT_LENGTH} caracteres` });
    }

    const entry = await prisma.journalEntry.create({
      data: { userId: req.user!.id, content: content.trim() }
    });
    res.status(201).json(entry);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Error guardando el diario' });
  }
});

app.get('/api/journal', auth, async (req, res) => {
  try {
    const entries = await prisma.journalEntry.findMany({
      where: { userId: req.user!.id },
      orderBy: { createdAt: 'desc' },
      take: 50
    });
    res.json(entries);
  } catch (error) {
    res.status(500).json({ error: 'Error consultando el diario' });
  }
});

// --- NOVA (IA) ---

function startOfToday(): Date {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

async function generateNovaReply(history: { role: string; content: string }[]): Promise<string> {
  if (!openai) {
    return "(Simulación, sin API Key) Entiendo cómo te sientes. ¿Qué crees que podrías hacer hoy por ti mismo para dar un pequeño paso?";
  }

  const completion = await openai.chat.completions.create({
    model: "gpt-4o-mini",
    messages: [
      { role: "system", content: NOVA_SYSTEM_PROMPT },
      ...history.map((m) => ({
        role: m.role === 'assistant' ? 'assistant' as const : 'user' as const,
        content: m.content
      }))
    ],
    max_tokens: 300,
    temperature: 0.5,
  });

  return completion.choices[0]?.message?.content || "No pude procesar tu mensaje.";
}

// Envía un mensaje a NOVA. Si no se indica sessionId, crea una sesión nueva.
// Responde { sessionId, role, content, crisis }.
app.post('/api/chat/nova', auth, async (req, res) => {
  try {
    const userId = req.user!.id;
    const { message, sessionId } = req.body;
    if (typeof message !== 'string' || message.trim() === '') {
      return res.status(400).json({ error: 'message es obligatorio' });
    }
    if (message.length > MAX_TEXT_LENGTH) {
      return res.status(400).json({ error: `message no puede superar ${MAX_TEXT_LENGTH} caracteres` });
    }

    let session = null;
    if (sessionId !== undefined && sessionId !== null) {
      session = await prisma.chatSession.findFirst({ where: { id: String(sessionId), userId } });
      if (!session) {
        return res.status(404).json({ error: 'Sesión no encontrada' });
      }
    }

    const crisis = isCrisisMessage(message);

    // El límite diario nunca bloquea un mensaje de crisis.
    if (!crisis) {
      const sentToday = await prisma.chatMessage.count({
        where: { role: 'user', createdAt: { gte: startOfToday() }, session: { userId } }
      });
      if (sentToday >= NOVA_DAILY_LIMIT) {
        return res.status(429).json({
          error: `Llegaste al límite de ${NOVA_DAILY_LIMIT} mensajes de hoy con NOVA. Puedes volver mañana o escribir en tu diario.`
        });
      }
    }

    if (!session) {
      session = await prisma.chatSession.create({ data: { userId } });
    }

    await prisma.chatMessage.create({
      data: { sessionId: session.id, role: 'user', content: message.trim() }
    });

    let reply: string;
    if (crisis) {
      reply = CRISIS_RESPONSE;
    } else {
      const recent = await prisma.chatMessage.findMany({
        where: { sessionId: session.id },
        orderBy: { createdAt: 'desc' },
        take: NOVA_HISTORY_LENGTH
      });
      reply = await generateNovaReply(recent.reverse());
    }

    await prisma.chatMessage.create({
      data: { sessionId: session.id, role: 'assistant', content: reply }
    });

    res.json({ sessionId: session.id, role: 'assistant', content: reply, crisis });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Error comunicando con NOVA' });
  }
});

// Historial de una sesión de NOVA del usuario.
app.get('/api/chat/sessions/:id/messages', auth, async (req, res) => {
  try {
    const session = await prisma.chatSession.findFirst({
      where: { id: String(req.params.id), userId: req.user!.id },
      include: { messages: { orderBy: { createdAt: 'asc' } } }
    });
    if (!session) {
      return res.status(404).json({ error: 'Sesión no encontrada' });
    }
    res.json(session.messages);
  } catch (error) {
    res.status(500).json({ error: 'Error consultando la conversación' });
  }
});

app.listen(PORT, () => {
  console.log(`Servidor de Senda corriendo en http://localhost:${PORT}`);
});
