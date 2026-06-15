import { useState, useEffect } from 'react'
import ChatWindow from './components/ChatWindow'
import './App.css'

function App() {
  const [sessionId, setSessionId] = useState('')

  useEffect(() => {
    // Generate a simple UUID for the session
    const newSessionId = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
      const r = Math.random() * 16 | 0
      const v = c === 'x' ? r : (r & 0x3 | 0x8)
      return v.toString(16)
    })
    setSessionId(newSessionId)
  }, [])

  return (
    <div className="app">
      <ChatWindow sessionId={sessionId} />
    </div>
  )
}

export default App
