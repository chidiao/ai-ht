import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export function fetchContracts(params) {
  return http.get('/contracts', { params }).then((response) => response.data.content || response.data)
}

export function fetchContractPage(params) {
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

export function deleteContract(id) {
  return http.delete(`/contracts/${id}`).then((response) => response.data)
}

export function runContractAction(id, payload) {
  return http.post(`/contracts/${id}/action`, payload).then((response) => response.data)
}

export function fetchContractStats() {
  return http.get('/contracts/stats').then((response) => response.data)
}

export function addContractPaymentPlan(id, payload) {
  return http.post(`/contracts/${id}/payment-plans`, payload).then((response) => response.data)
}

export function addContractAttachment(id, payload) {
  return http.post(`/contracts/${id}/attachments`, payload).then((response) => response.data)
}

export function addContractAcceptance(id, payload) {
  return http.post(`/contracts/${id}/acceptances`, payload).then((response) => response.data)
}
