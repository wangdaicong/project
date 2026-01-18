import { http } from './http'
import type { ApiResponse } from './health'

export type Paper = {
  id: number
  stageId: number
  subjectId: number
  title: string
  paperDate: string
  regionCode?: string
}

export type ListPapersResponse = {
  items: Paper[]
}

export async function listPapers(
  stageId: number,
  subjectId: number,
  grade?: number,
  regionCode?: string
): Promise<ApiResponse<ListPapersResponse>> {
  const resp = await http.get<ApiResponse<ListPapersResponse>>('/api/papers', { params: { stageId, subjectId, grade, regionCode } })
  return resp.data
}

export async function getPaperDetail(id: number): Promise<ApiResponse<Paper>> {
  const resp = await http.get<ApiResponse<any>>(`/api/papers/${id}`)
  return resp.data as any
}

export async function incPaperDownload(id: number): Promise<ApiResponse<number>> {
  const resp = await http.post<ApiResponse<number>>(`/api/papers/${id}/download`)
  return resp.data as any
}
