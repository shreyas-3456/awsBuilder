const FUNCTION_PATH = '/.netlify/functions/backend-proxy';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
};

const hopByHopHeaders = new Set([
  'connection',
  'content-encoding',
  'content-length',
  'host',
  'transfer-encoding',
]);

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
  const targetUrl = new URL(proxiedPath || '/', backendBaseUrl);
  targetUrl.search = event.rawQuery ? `?${event.rawQuery}` : '';

  const requestHeaders = { ...event.headers };
  for (const headerName of Object.keys(requestHeaders)) {
    if (hopByHopHeaders.has(headerName.toLowerCase())) {
      delete requestHeaders[headerName];
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
      if (!hopByHopHeaders.has(key.toLowerCase())) {
        responseHeaders[key] = value;
      }
    });

    return {
      statusCode: response.status,
      headers: {
        ...responseHeaders,
        ...corsHeaders,
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
