import { http } from './http'
import type { ApiResponse } from './health'

export type EduStage = {
  id: number
  code: string
  name: string
  status: number
  sort: number
}

export type Subject = {
  id: number
  stageId: number
  code: string
  name: string
  status: number
  sort: number
}

export async function listStages(): Promise<ApiResponse<EduStage[]>> {
  const resp = await http.get<ApiResponse<EduStage[]>>('/api/meta/stages')
  return resp.data
}

export async function listSubjects(stageId: number): Promise<ApiResponse<Subject[]>> {
  const resp = await http.get<ApiResponse<Subject[]>>('/api/meta/subjects', { params: { stageId } })
  return resp.data
}
