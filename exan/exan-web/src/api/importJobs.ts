import { http } from './http'
import type { ApiResponse } from './health'
import type { ImportQuestionItem, Question } from './adminQuestions'

export type ImportJob = {
  id: number
  jobType: string
  stageId?: number
  subjectId?: number
  status: string
  totalCount: number
  insertedCount: number
  duplicateCount: number
  failedCount: number
  originalFilename?: string
  storedFilePath?: string
  createdAt: string
}

export type CreateImportJobResponse = {
  jobId: number
  totalCount: number
  insertedCount: number
  duplicateCount: number
  failedCount: number
}

export async function createJobFromJson(items: ImportQuestionItem[]): Promise<ApiResponse<CreateImportJobResponse>> {
  const resp = await http.post<ApiResponse<CreateImportJobResponse>>('/admin/import-jobs/question-json', { items })
  return resp.data
}

export async function createJobFromJsonFile(file: File): Promise<ApiResponse<CreateImportJobResponse>> {
  const form = new FormData()
  form.append('file', file)
  const resp = await http.post<ApiResponse<CreateImportJobResponse>>('/admin/import-jobs/question-json-file', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return resp.data
}

export async function listJobs(limit = 20): Promise<ApiResponse<ImportJob[]>> {
  const resp = await http.get<ApiResponse<ImportJob[]>>('/admin/import-jobs', { params: { limit } })
  return resp.data
}

export async function listPendingQuestionsByJob(jobId: number, limit = 200): Promise<ApiResponse<Question[]>> {
  const resp = await http.get<ApiResponse<Question[]>>(`/admin/import-jobs/${jobId}/pending-questions`, {
    params: { limit }
  })
  return resp.data
}

export async function approveAllByJob(jobId: number): Promise<ApiResponse<number>> {
  const resp = await http.post<ApiResponse<number>>(`/admin/import-jobs/${jobId}/approve-all`)
  return resp.data
}

export async function rejectAllByJob(jobId: number): Promise<ApiResponse<number>> {
  const resp = await http.post<ApiResponse<number>>(`/admin/import-jobs/${jobId}/reject-all`)
  return resp.data
}
