
import { NextApiRequest, NextApiResponse } from 'next';
import axios, {AxiosRequestConfig, AxiosResponse} from 'axios';

interface ErrorResponse {
    error: string;
}

type ProxyResponse<R> = AxiosResponse<R> | ErrorResponse;

const proxy = async <T, R>(req: NextApiRequest, res: NextApiResponse<ProxyResponse<R>>) => {
    try {
        const baseUrl = process.env.NEXT_PUBLIC_BACKEND_BASE_URL; // FIXME move to const
        if (!baseUrl) {
            throw new Error('Backend base URL is not set!');
        }
        const requestUrl = req.url!.replace('/api/proxy', '');
        const url = new URL(`${baseUrl}${requestUrl}`);
        const config: AxiosRequestConfig = {
            method: req.method,
            url: url.toString(),
            headers: req.headers,
        };
        if (req.method !== 'GET' && req.method !== 'HEAD') {
            config.data = req.body as T;
        }
        const response = await axios(config);
        return res.status(response.status).json(response.data);
    } catch (error) {
        console.error(error);
        return res.status(500).json({ error: 'Internal Server Error' });
    }
};

export default proxy;