import axios, { AxiosResponse } from 'axios';

class FetchError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'FetchError';
    }
}

class RestService {
    private readonly baseUrl: string | undefined;
    private readonly proxyUrl: string;

    constructor() {
        this.baseUrl = process.env.NEXT_PUBLIC_BACKEND_BASE_URL; //FIXME remove, not needed here
        if (!this.baseUrl) {
            throw new Error('Backend base URL is not set!');
        }
        this.proxyUrl = '/api/proxy/';
        console.log(this.baseUrl);
    }

    private getProxyUrl(endpoint: string): string {
        return `${this.proxyUrl}${endpoint}`;
    }

    public async get<R>(endpoint: string): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.getProxyUrl(endpoint);
            return await axios.get(proxyUrl);
        } catch (error) {
            throw new FetchError(`GET request failed: ${error}`);
        }
    }

    public async post<T, R>(endpoint: string, data: T): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.getProxyUrl(endpoint);
            return await axios.post(proxyUrl, data);
        } catch (error) {
            throw new FetchError(`POST request failed: ${error}`);
        }
    }

    public async put<T, R>(endpoint: string, data: T): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.getProxyUrl(endpoint);
            return await axios.put(proxyUrl, data);
        } catch (error) {
            throw new FetchError(`PUT request failed: ${error}`);
        }
    }
}

export default RestService;