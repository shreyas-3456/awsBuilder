import axios from 'axios';
import { DiagramDTO, TerraformResponse, ErrorResponse } from '../types/diagram';

const API_BASE_URL = '/api';

class DiagramService {
  async generateTerraform(diagram: DiagramDTO): Promise<TerraformResponse> {
    try {
      const response = await axios.post<TerraformResponse>(
        `${API_BASE_URL}/diagrams/generate`,
        diagram,
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const errorData = error.response.data as ErrorResponse;
        throw new Error(errorData.error + (errorData.details ? ': ' + errorData.details.join(', ') : ''));
      }
      throw new Error('Failed to generate Terraform code. Please check your connection.');
    }
  }
}

export const diagramService = new DiagramService();
