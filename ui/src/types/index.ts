export interface Message {
  id: string | number;
  content: string;
  isUser: boolean;
  timestamp: Date;
}

export interface Conversation {
  conversationId: string;
  title: string;
  createTime?: string;
}

export interface Settings {
  userId: string;
  userName: string;
  streamMode: boolean;
  autoScroll: boolean;
  showTimestamp: boolean;
  apiBaseUrl: string;
  timeout: number;
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface ConversationApiResponse {
  conversations: Conversation[];
}

export interface CreateConversationResponse {
  conversationId: string;
  title: string;
}

export interface ConversationMessagesResponse {
  messages: Array<{
    id: string;
    content: string;
    isUser: boolean;
    timestamp: string | [number, number, number, number, number, number, number];
  }>;
}