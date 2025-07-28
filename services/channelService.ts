import { apiService, Channel, User } from './api';

export interface ChannelWithUI extends Channel {
  unread?: boolean;
  lastMessage?: string;
  timestamp?: string;
}

class ChannelService {
  async getUserChannels(): Promise<ChannelWithUI[]> {
    const channels = await apiService.getUserChannels();
    return channels.map(channel => ({
      ...channel,
      unread: false, // TODO: Implement unread status from backend
      lastMessage: 'No messages yet',
      timestamp: new Date(channel.updatedAt).toLocaleDateString(),
    }));
  }

  async createChannel(name: string, description?: string, isPrivate: boolean = false): Promise<Channel> {
    return apiService.createChannel(name, description, isPrivate);
  }

  async joinChannel(channelId: number): Promise<void> {
    await apiService.joinChannel(channelId);
  }
}

export const channelService = new ChannelService();