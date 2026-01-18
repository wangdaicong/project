import { http } from './http'
import type { ApiResponse } from './health'

export async function createPracticeSession(payload: {
  stageId: number
  subjectId: number
  count: number
}): Promise<ApiResponse<number>> {
  const resp = await http.post<ApiResponse<number>>('/api/practice/sessions', payload)
  return resp.data
}

export type SessionQuestionVO = {
  id: number
  type: string
  stem: string
  difficulty: number
  options: { key: string; content: string }[]
}

export type SessionDetailResponse = {
  sessionId: number
  stageId: number
  subjectId: number
  status: string
  scoreGot?: number
  scoreTotal?: number
  questions: SessionQuestionVO[]
}

export async function getPracticeSessionDetail(id: number): Promise<ApiResponse<SessionDetailResponse>> {
  const resp = await http.get<ApiResponse<SessionDetailResponse>>(`/api/practice/sessions/${id}`)
  return resp.data
}

export async function submitAnswer(sessionId: number, payload: { questionId: number; answer: any }): Promise<ApiResponse<null>> {
  const resp = await http.post<ApiResponse<null>>(`/api/practice/sessions/${sessionId}/answers`, payload)
  return resp.data
}

export async function submitSession(sessionId: number): Promise<ApiResponse<SessionDetailResponse>> {
  const resp = await http.post<ApiResponse<SessionDetailResponse>>(`/api/practice/sessions/${sessionId}/submit`)
  return resp.data
}
