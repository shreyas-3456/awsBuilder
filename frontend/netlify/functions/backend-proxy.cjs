const FUNCTION_PATH = '/.netlify/functions/backend-proxy';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
};

const requestHeadersToForward = new Set([
  'accept',
  'authorization',
  'content-type',
]);

const responseHeadersToForward = new Set([
  'content-type',
]);

function buildTargetUrl(backendBaseUrl, proxiedPath, rawQuery) {
  const baseUrl = new URL(backendBaseUrl);
  const basePath = baseUrl.pathname.replace(/\/$/, '');
  const requestPath = (proxiedPath || '/').replace(/^\//, '');

  baseUrl.pathname = [basePath, requestPath].filter(Boolean).join('/');
  baseUrl.search = rawQuery ? `?${rawQuery}` : '';

  return baseUrl;
}

exports.handler = async (event) => {
  if (event.httpMethod === 'OPTIONS') {
    return {
      statusCode: 204,
      headers: corsHeaders,
      body: '',
    };
  }

  const backendBaseUrl = process.env.BACKEND_URL || process.env.EC2_BACKEND_URL;

  if (!backendBaseUrl) {
    return {
      statusCode: 500,
      headers: {
        ...corsHeaders,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        error: 'Missing BACKEND_URL. Set it in Netlify environment variables.',
      }),
    };
  }

  const proxiedPath = event.path.startsWith(FUNCTION_PATH)
    ? event.path.slice(FUNCTION_PATH.length)
    : event.path;
  const targetUrl = buildTargetUrl(backendBaseUrl, proxiedPath, event.rawQuery);

  const requestHeaders = {};
  for (const [headerName, headerValue] of Object.entries(event.headers || {})) {
    if (requestHeadersToForward.has(headerName.toLowerCase())) {
      requestHeaders[headerName] = headerValue;
    }
  }

  try {
    const response = await fetch(targetUrl, {
      method: event.httpMethod,
      headers: requestHeaders,
      body: event.body
        ? Buffer.from(event.body, event.isBase64Encoded ? 'base64' : 'utf8')
        : undefined,
    });

    const responseHeaders = {};
    response.headers.forEach((value, key) => {
      if (responseHeadersToForward.has(key.toLowerCase())) {
        responseHeaders[key] = value;
      }
    });

    return {
      statusCode: response.status,
      headers: {
        ...responseHeaders,
        ...corsHeaders,
        'x-proxied-url': targetUrl.toString(),
      },
      body: await response.text(),
    };
  } catch (error) {
    return {
      statusCode: 502,
      headers: {
        ...corsHeaders,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        error: 'Failed to reach backend',
        details: error instanceof Error ? error.message : String(error),
      }),
    };
  }
};
