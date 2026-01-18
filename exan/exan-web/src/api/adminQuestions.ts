import { http } from './http'
import type { ApiResponse } from './health'

export type ImportQuestionOptionItem = {
  key: string
  content: string
}

export type ImportQuestionItem = {
  stageId: number
  subjectId: number
  type: string
  stem: string
  difficulty?: number
  analysis?: string
  answer?: any
  options?: ImportQuestionOptionItem[]
}

export async function importQuestions(items: ImportQuestionItem[]): Promise<ApiResponse<null>> {
  const resp = await http.post<ApiResponse<null>>('/admin/questions/import', { items })
  return resp.data
}

export type Question = {
  id: number
  stageId: number
  subjectId: number
  type: string
  stem: string
  difficulty: number
  analysis?: string
  answer?: string
  status: string
}

export async function listPendingQuestions(subjectId: number, limit = 50): Promise<ApiResponse<Question[]>> {
  const resp = await http.get<ApiResponse<Question[]>>('/admin/questions/pending', { params: { subjectId, limit } })
  return resp.data
}

export async function approveQuestion(id: number): Promise<ApiResponse<null>> {
  const resp = await http.post<ApiResponse<null>>(`/admin/questions/${id}/approve`)
  return resp.data
}

export async function rejectQuestion(id: number): Promise<ApiResponse<null>> {
  const resp = await http.post<ApiResponse<null>>(`/admin/questions/${id}/reject`)
  return resp.data
}
