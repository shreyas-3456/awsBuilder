declare const process: {
  env: {
    NODE_ENV?: string;
  };
};

export const API_BASE_URL =
  process.env.NODE_ENV === 'production'
    ? '/.netlify/functions/backend-proxy/api'
    : '/api';
