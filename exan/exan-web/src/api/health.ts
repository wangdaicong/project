import { http } from './http'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export async function healthCheck(): Promise<ApiResponse<string>> {
  const resp = await http.get<ApiResponse<string>>('/api/health')
  return resp.data
}
