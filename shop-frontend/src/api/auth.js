import request from '../utils/request'

export function login(data) { return request.post('/auth/login', data) }
export function register(data) { return request.post('/auth/register', data) }
export function refresh(data) { return request.post('/auth/refresh', data) }
export function logoutReq(data) { return request.post('/auth/logout', data) }
export function sendCode(data) { return request.post('/auth/send-code', data) }
export function loginBySms(data) { return request.post('/auth/login-by-sms', data) }
export function registerBySms(data) { return request.post('/auth/register-by-sms', data) }
