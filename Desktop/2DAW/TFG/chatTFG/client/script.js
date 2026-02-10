import { io } from 'https://cdn.socket.io/4.8.1/socket.io.esm.min.js';
const socket = io();

const form = document.querySelector(".form");
const input = document.getElementById("input");
const messages = document.getElementById("messages");

const roleInput = "admin";
const userNameInput = "Joel";

function addMessageToUI(msg) {
  const item = document.createElement("li");
  item.innerHTML = `<strong>${msg.username}:</strong> ${msg.content}`;
  messages.appendChild(item);

  // auto-scroll
  messages.scrollTop = messages.scrollHeight;
}

// 1) Historial desde DB
socket.on('chat history', (rows) => {
  messages.innerHTML = "";
  rows.forEach(addMessageToUI);
});

// 2) Mensajes nuevos
socket.on('chat message', (msg) => {
  addMessageToUI(msg);
});

// 3) Enviar mensaje
form.addEventListener('submit', (e) => {
  e.preventDefault();

  const content = input.value.trim();
  if (!content) return;

  const msg = {
    content,
    role: roleInput || 'Usuario',
    username: userNameInput || 'Desconocido'
  };

  socket.emit('chat message', msg);
  input.value = '';
});
