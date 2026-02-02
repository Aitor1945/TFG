import express from 'express';
import logger from 'morgan';
import { Server } from 'socket.io';
import { createServer } from 'node:http';
import path from 'node:path';

import sqlite3 from 'sqlite3';

const port = process.env.PORT ?? 3000;

const app = express();
const server = createServer(app);
const io = new Server(server);

app.use(logger('dev'));

const clientPath = path.join(process.cwd(), 'client');
app.use(express.static(clientPath));

app.get('/', (req, res) => {
  res.sendFile(path.join(clientPath, 'index.html'));
});

/*DB (SQLite)  */
const db = new sqlite3.Database(path.join(process.cwd(), 'chat.db'));

db.serialize(() => {
  db.run(`
    CREATE TABLE IF NOT EXISTS messages (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      content TEXT NOT NULL,
      role TEXT NOT NULL,
      username TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);
});

/* Socket*/
io.on('connection', (socket) => {
  console.log('Un usuario se ha conectado');

  // 1) Enviar historial al conectar
  db.all(
    `SELECT id, content, role, username, created_at
     FROM messages
     ORDER BY id ASC
     LIMIT 200`,
    (err, rows) => {
      if (err) {
        console.error('Error cargando historial:', err);
        return;
      }
      socket.emit('chat history', rows);
    }
  );

  // 2) Guardar cada mensaje en DB y emitirlo
  socket.on('chat message', (msg) => {
    const content = String(msg?.content ?? '').trim();
    const role = String(msg?.role ?? 'Usuario').trim();
    const username = String(msg?.username ?? 'Desconocido').trim();

    if (!content) return;

    db.run(
      `INSERT INTO messages (content, role, username) VALUES (?, ?, ?)`,
      [content, role, username],
      function (err) {
        if (err) {
          console.error('Error guardando mensaje:', err);
          return;
        }

        const savedMsg = {
          id: this.lastID,
          content,
          role,
          username,
          created_at: new Date().toISOString()
        };

        io.emit('chat message', savedMsg);
      }
    );
  });

  socket.on('disconnect', () => {
    console.log('Un usuario se ha desconectado');
  });
});

server.listen(port, () => {
  console.log(`Servidor corriendo en puerto ${port}`);
});
