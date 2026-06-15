import { useState, useRef, useEffect } from 'react'
import MessageList from './MessageList'
import ChatInput from './ChatInput'
import './ChatWindow.css'

function ChatWindow({ sessionId }) {
  const [messages, setMessages] = useState([
    { id: 1, text: 'Welcome! Start chatting with the AI agent. You can ask about weather, locations, and more!', sender: 'system' }
  ])
  const [loading, setLoading] = useState(false)
  const messagesEndRef = useRef(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const sendMessage = async (text) => {
    if (!text.trim()) return

    // Add user message
    const userMessage = { id: Date.now(), text, sender: 'user' }
    setMessages(prev => [...prev, userMessage])
    setLoading(true)

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sessionId, message: text })
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      const data = await response.json()

      if (data.error) {
        setMessages(prev => [...prev, {
          id: Date.now(),
          text: `Error: ${data.error}`,
          sender: 'error'
        }])
      } else {
        setMessages(prev => [...prev, {
          id: Date.now(),
          text: data.reply || 'No response',
          sender: 'assistant'
        }])
      }
    } catch (error) {
      setMessages(prev => [...prev, {
        id: Date.now(),
        text: `Connection error: ${error.message}. Make sure the agent is running on port 7171.`,
        sender: 'error'
      }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="chat-container">
      <div className="chat-header">
        <h1>MCP Chat Agent</h1>
        <p className="session-id">Session: <span>{sessionId?.substring(0, 8)}...</span></p>
      </div>

      <MessageList messages={messages} messagesEndRef={messagesEndRef} />

      <ChatInput onSend={sendMessage} disabled={loading} />

      <div className="chat-footer">
        <small>Powered by Spring AI + MCP Server</small>
      </div>
    </div>
  )
}

export default ChatWindow
