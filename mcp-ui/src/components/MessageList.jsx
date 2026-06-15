import './ChatWindow.css'

function MessageList({ messages, messagesEndRef }) {
  return (
    <div className="chat-messages">
      {messages.map(msg => (
        <div key={msg.id} className={`message ${msg.sender}`}>
          <p>{msg.text}</p>
        </div>
      ))}
      <div ref={messagesEndRef} />
    </div>
  )
}

export default MessageList
