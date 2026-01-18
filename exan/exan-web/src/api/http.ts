import axios from 'axios'
import type { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

export const http = axios.create({
  timeout: 15000
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  config.headers = config.headers ?? {}
  if (!('X-User-Id' in config.headers)) {
    ;(config.headers as any)['X-User-Id'] = '1'
  }
  return config
})

http.interceptors.response.use(
  (resp: AxiosResponse) => resp,
  (err: AxiosError) => {
    return Promise.reject(err)
  }
)
