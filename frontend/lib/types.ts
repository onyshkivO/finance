export type TransactionType = "income" | "expense"
export interface UserData {
    token: string;
    login: string;
    currency: string;
    id: string;
}
export interface Category {
    id: string; 
    name: string;
    icon: string;
    type: string;
    mccCodes: Set<number>;
  }
  