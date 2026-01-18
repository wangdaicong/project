import { http } from './http'
import type { ApiResponse } from './health'

export type LeaderboardItem = {
  userId: number
  score: number
  rank: number
}

export async function getDailyLeaderboard(subjectId: number, limit = 20): Promise<ApiResponse<LeaderboardItem[]>> {
  const resp = await http.get<ApiResponse<LeaderboardItem[]>>('/api/leaderboards/daily', { params: { subjectId, limit } })
  return resp.data
}
