import AsyncStorage from '@react-native-async-storage/async-storage';

const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080/api';

export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  profilePicture?: string;
  status: 'ACTIVE' | 'AWAY' | 'DO_NOT_DISTURB' | 'OFFLINE';
  isOnline: boolean;
  lastSeen?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Channel {
  id: number;
  name: string;
  description?: string;
  isPrivate: boolean;
  createdBy: User;
  members: User[];
  createdAt: string;
  updatedAt: string;
}

export interface Message {
  id: number;
  content: string;
  sender: User;
  channel?: Channel;
  recipient?: User;
  parentMessage?: Message;
  replies: Message[];
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'AUDIO' | 'VIDEO';
  isEdited: boolean;
  editedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

class ApiService {
  private async getAuthHeaders(): Promise<Record<string, string>> {
    const token = await AsyncStorage.getItem('authToken');
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  }

  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: 'Network error' }));
      throw new Error(errorData.message || `HTTP ${response.status}`);
    }
    return response.json();
  }

  // Authentication
  async login(username: string, password: string): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/signin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    
    const data = await this.handleResponse<AuthResponse>(response);
    await AsyncStorage.setItem('authToken', data.accessToken);
    await AsyncStorage.setItem('userId', data.id.toString());
    return data;
  }

  async signup(username: string, email: string, password: string): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password }),
    });
    
    return this.handleResponse(response);
  }

  async logout(): Promise<void> {
    try {
      await fetch(`${API_BASE_URL}/auth/signout`, {
        method: 'POST',
        headers: await this.getAuthHeaders(),
      });
    } finally {
      await AsyncStorage.multiRemove(['authToken', 'userId']);
    }
  }

  // User Management
  async getUserProfile(): Promise<User> {
    const response = await fetch(`${API_BASE_URL}/users/profile`, {
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse<User>(response);
  }

  async getOnlineUsers(): Promise<User[]> {
    const response = await fetch(`${API_BASE_URL}/users/online`, {
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse<User[]>(response);
  }

  async updateUserStatus(status: string, isOnline?: boolean): Promise<{ message: string }> {
    const params = new URLSearchParams({ status });
    if (isOnline !== undefined) {
      params.append('isOnline', isOnline.toString());
    }

    const response = await fetch(`${API_BASE_URL}/users/status?${params}`, {
      method: 'PUT',
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  async updateFirebaseToken(token: string): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/users/firebase-token?token=${encodeURIComponent(token)}`, {
      method: 'PUT',
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  // Channels
  async getUserChannels(): Promise<Channel[]> {
    const response = await fetch(`${API_BASE_URL}/channels`, {
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse<Channel[]>(response);
  }

  async createChannel(name: string, description?: string, isPrivate: boolean = false): Promise<Channel> {
    const response = await fetch(`${API_BASE_URL}/channels`, {
      method: 'POST',
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({ name, description, isPrivate }),
    });
    
    return this.handleResponse<Channel>(response);
  }

  async joinChannel(channelId: number): Promise<{ message: string }> {
    const response = await fetch(`${API_BASE_URL}/channels/${channelId}/join`, {
      method: 'POST',
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse(response);
  }

  // Messages
  async getChannelMessages(channelId: number, page: number = 0, size: number = 20): Promise<{
    content: Message[];
    totalElements: number;
    totalPages: number;
    last: boolean;
  }> {
    const response = await fetch(
      `${API_BASE_URL}/messages/channel/${channelId}?page=${page}&size=${size}`,
      { headers: await this.getAuthHeaders() }
    );
    
    return this.handleResponse(response);
  }

  async getDirectMessages(userId: number, page: number = 0, size: number = 20): Promise<{
    content: Message[];
    totalElements: number;
    totalPages: number;
    last: boolean;
  }> {
    const response = await fetch(
      `${API_BASE_URL}/messages/direct/${userId}?page=${page}&size=${size}`,
      { headers: await this.getAuthHeaders() }
    );
    
    return this.handleResponse(response);
  }

  async sendChannelMessage(channelId: number, content: string, type: string = 'TEXT'): Promise<Message> {
    const response = await fetch(`${API_BASE_URL}/messages/channel/${channelId}`, {
      method: 'POST',
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({ content, type }),
    });
    
    return this.handleResponse<Message>(response);
  }

  async sendDirectMessage(userId: number, content: string, type: string = 'TEXT'): Promise<Message> {
    const response = await fetch(`${API_BASE_URL}/messages/direct/${userId}`, {
      method: 'POST',
      headers: await this.getAuthHeaders(),
      body: JSON.stringify({ content, type }),
    });
    
    return this.handleResponse<Message>(response);
  }

  async getThreadReplies(messageId: number): Promise<Message[]> {
    const response = await fetch(`${API_BASE_URL}/messages/${messageId}/replies`, {
      headers: await this.getAuthHeaders(),
    });
    
    return this.handleResponse<Message[]>(response);
  }

  // Utility methods
  async isAuthenticated(): Promise<boolean> {
    const token = await AsyncStorage.getItem('authToken');
    return !!token;
  }

  async getCurrentUserId(): Promise<number | null> {
    const userId = await AsyncStorage.getItem('userId');
    return userId ? parseInt(userId, 10) : null;
  }
}

export const apiService = new ApiService();