import axios, { AxiosResponse } from 'axios';

class FetchError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'FetchError';
    }
}

class RestService {
    private readonly baseProxyUrl: string;

    constructor() {
        this.baseProxyUrl = '/api/proxy/';
    }

    private buildProxyUrl(endpoint: string): string {
        return `${this.baseProxyUrl}${endpoint}`;
    }

    public async get<R>(endpoint: string): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.buildProxyUrl(endpoint);
            return await axios.get(proxyUrl);
        } catch (error) {
            throw new FetchError(`GET request failed: ${error}`);
        }
    }

    public async post<T, R>(endpoint: string, data: T): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.buildProxyUrl(endpoint);
            return await axios.post(proxyUrl, data);
        } catch (error) {
            throw new FetchError(`POST request failed: ${error}`);
        }
    }

    public async put<T, R>(endpoint: string, data: T): Promise<AxiosResponse<R>> {
        try {
            const proxyUrl = this.buildProxyUrl(endpoint);
            return await axios.put(proxyUrl, data);
        } catch (error) {
            throw new FetchError(`PUT request failed: ${error}`);
        }
    }
}

export default RestService;