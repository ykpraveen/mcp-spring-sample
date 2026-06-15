import { useState } from 'react'

function ChatInput({ onSend, disabled }) {
  const [input, setInput] = useState('')

  const handleSend = () => {
    onSend(input)
    setInput('')
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="chat-input-area">
      <input
        type="text"
        placeholder="Type your message..."
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyPress={handleKeyPress}
        disabled={disabled}
        autoFocus
      />
      <button onClick={handleSend} disabled={disabled}>
        Send
      </button>
    </div>
  )
}

export default ChatInput
