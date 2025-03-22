interface BackendErrorsProps {
    message: string | null;
    name: string;
    timestamp: string | null;
  }

  export function BackendErrors( { error }: { readonly error: BackendErrorsProps }) {
    if (!error?.message) return null;
    return <div className="text-pink-500 text-md italic mt-1 py-2">{error.message}</div>;
  }