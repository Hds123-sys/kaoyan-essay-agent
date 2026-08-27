import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([])

  // Load from localStorage on initialization
  const loadFromStorage = () => {
    try {
      const stored = localStorage.getItem('chat_history')
      if (stored) {
        messages.value = JSON.parse(stored)
      }
    } catch (e) {
      console.error('Failed to load chat history from localStorage:', e)
    }
  }

  // Save to localStorage
  const saveToStorage = () => {
    try {
      localStorage.setItem('chat_history', JSON.stringify(messages.value))
    } catch (e) {
      console.error('Failed to save chat history to localStorage:', e)
    }
  }

  // Add user message
  const addUserMessage = (content, essayType) => {
    messages.value.push({
      role: 'user',
      type: 'user',
      content,
      essayType,
      time: new Date().toLocaleString()
    })
    saveToStorage()
  }

  // Add assistant message (correction result)
  const addAssistantMessage = (result, meta) => {
    messages.value.push({
      role: 'assistant',
      type: 'assistant',
      content: '批改完成',
      result,
      meta,
      time: new Date().toLocaleString()
    })
    saveToStorage()
  }

  // Add system message
  const addSystemMessage = (content) => {
    messages.value.push({
      role: 'system',
      type: 'system',
      content,
      time: new Date().toLocaleString()
    })
    saveToStorage()
  }

  // Clear messages
  const clearMessages = () => {
    messages.value = []
    localStorage.removeItem('chat_history')
  }

  // Get the last message
  const getLastMessage = () => {
    return messages.value[messages.value.length - 1] || null
  }

  // Initialize
  loadFromStorage()

  return {
    messages,
    addUserMessage,
    addAssistantMessage,
    addSystemMessage,
    clearMessages,
    getLastMessage
  }
})