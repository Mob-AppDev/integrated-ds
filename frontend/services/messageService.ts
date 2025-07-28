import { apiService, Message, Channel, User } from './api';

export interface MessageWithUI extends Message {
  isCurrentUser: boolean;
  senderAvatar: string;
  timestamp: string;
  reactions?: Array<{ emoji: string; count: number; users: string[] }>;
  hasImage?: boolean;
  imageUrl?: string;
  attachments?: Array<{ type: 'image' | 'file'; name: string; url: string; mimeType?: string }>;
}

class MessageService {
  private currentUserId: number | null = null;

  async initialize() {
    this.currentUserId = await apiService.getCurrentUserId();
  }

  private formatMessage(message: Message): MessageWithUI {
    const isCurrentUser = message.sender.id === this.currentUserId;
    const timestamp = new Date(message.createdAt).toLocaleTimeString([], { 
      hour: '2-digit', 
      minute: '2-digit' 
    });

    return {
      ...message,
      isCurrentUser,
      senderAvatar: message.sender.username.charAt(0).toUpperCase(),
      timestamp,
      reactions: [], // TODO: Implement reactions in backend
    };
  }

  async getChannelMessages(channelId: number, page: number = 0): Promise<MessageWithUI[]> {
    await this.initialize();
    const response = await apiService.getChannelMessages(channelId, page);
    return response.content.map(msg => this.formatMessage(msg)).reverse(); // Reverse for chronological order
  }

  async getDirectMessages(userId: number, page: number = 0): Promise<MessageWithUI[]> {
    await this.initialize();
    const response = await apiService.getDirectMessages(userId, page);
    return response.content.map(msg => this.formatMessage(msg)).reverse();
  }

  async sendChannelMessage(channelId: number, content: string): Promise<MessageWithUI> {
    await this.initialize();
    const message = await apiService.sendChannelMessage(channelId, content);
    return this.formatMessage(message);
  }

  async sendDirectMessage(userId: number, content: string): Promise<MessageWithUI> {
    await this.initialize();
    const message = await apiService.sendDirectMessage(userId, content);
    return this.formatMessage(message);
  }

  async getThreadReplies(messageId: number): Promise<MessageWithUI[]> {
    await this.initialize();
    const replies = await apiService.getThreadReplies(messageId);
    return replies.map(msg => this.formatMessage(msg));
  }
}

export const messageService = new MessageService(); 