import request from '../utils/request'

export function getProfile() { return request.get('/user/profile') }
export function updateProfile(data) { return request.put('/user/profile', data) }
export function getAddresses() { return request.get('/user/addresses') }
export function addAddress(data) { return request.post('/user/addresses', data) }
