import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s'
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

function loginMecanico() {
  const payload = JSON.stringify({
    grant_type: 'password',
    username: 'mecanico@teste.com',
    password: '123456'
  });

  const res = http.post(`${baseUrl}/oauth/token`, payload, {
    headers: { 'Content-Type': 'application/json' }
  });

  check(res, {
    'login status 200': r => r.status === 200,
    'tem access_token': r => r.json('access_token')
  });

  return res.json('access_token');
}

export default function () {
  const token = loginMecanico();
  const headers = { Authorization: `Bearer ${token}` };

  const listaOs = http.get(`${baseUrl}/api/ordens-servico`, { headers });

  check(listaOs, {
    'listar OS status 200': r => r.status === 200
  });

  sleep(1);
}

