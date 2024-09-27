import axios, {AxiosResponse} from 'axios';

class RestService {
    private readonly baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    public async get(endpoint: string): Promise<AxiosResponse<any>> {
        try {
            return await axios.get(`${this.baseUrl}${endpoint}`);
        } catch (error) {
            throw new Error(`GET request failed: ${error}`);
        }
    }

    public async post(endpoint: string, data: any): Promise<AxiosResponse<any>> {
        try {
            return await axios.post(`${this.baseUrl}${endpoint}`, data);
        } catch (error) {
            throw new Error(`POST request failed: ${error}`);
        }
    }

    public async put(endpoint: string, data: any): Promise<AxiosResponse<any>> {
        try {
            return await axios.put(`${this.baseUrl}${endpoint}`, data);
        } catch (error) {
            throw new Error(`PUT request failed: ${error}`);
        }
    }
}

export default RestService;
