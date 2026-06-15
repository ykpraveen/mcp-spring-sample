# mcp-ui - React Chat Frontend

React 18 + Vite chat interface for the MCP Spring Agent.

## Setup

```bash
npm install
npm run dev          # Start on port 3000
npm run build        # Production build
```

## Configuration

Edit `vite.config.js`:
- `port: 3000` - Dev server port
- `target: 'http://localhost:7171'` - Agent URL

## Structure

```
src/
├── components/
│   ├── ChatWindow.jsx     # Main container
│   ├── ChatInput.jsx      # Input field
│   └── MessageList.jsx    # Messages
├── App.jsx                # Root component
└── main.jsx               # Entry point
```

## Features

- Real-time chat with agent
- Auto-proxy to agent API
- Session management
- Responsive design

For more details, see [parent README](../README.md).

## Deployment

### Option 1: Static Hosting (Netlify, Vercel, GitHub Pages)

```bash
npm run build
# Upload the `dist/` folder to your hosting service
```

### Option 2: Docker

```dockerfile
FROM node:20-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=builder /app/dist ./dist
EXPOSE 3000
CMD ["serve", "-s", "dist", "-l", "3000"]
```

## Troubleshooting

### Connection refused to agent
- Verify MCP Spring Agent is running: `mvn -C:mcp-spring-agent spring-boot:run`
- Check if port 7171 is correct in `vite.config.js`

### Session ID not displaying
- Open browser DevTools (F12) and check console for errors
- Verify the app is fetching data correctly

### Hot reload not working
- Check if `npm run dev` is running
- Try hard-refreshing the browser (Ctrl+Shift+R)

## Tech Stack

- **React 18** - UI framework
- **Vite 5** - Build tool & dev server
- **JavaScript ES6+** - Modern JavaScript
- **CSS3** - Styling with gradients and animations
