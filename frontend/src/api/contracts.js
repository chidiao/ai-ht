import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export function fetchContracts(params) {
  return http.get('/contracts', { params }).then((response) => response.data)
}

export function fetchContractDetail(id) {
  return http.get(`/contracts/${id}`).then((response) => response.data)
}

export function createContract(payload) {
  return http.post('/contracts', payload).then((response) => response.data)
}

export function updateContract(id, payload) {
  return http.put(`/contracts/${id}`, payload).then((response) => response.data)
}

export function runContractAction(id, payload) {
  return http.post(`/contracts/${id}/action`, payload).then((response) => response.data)
}

export function fetchContractStats() {
  return http.get('/contracts/stats').then((response) => response.data)
}
