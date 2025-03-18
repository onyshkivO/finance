import Logo from "@/components/custom/Logo";

export default function AuthLayout({ children }: {
    readonly children: React.ReactNode;
  }) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 dark:bg-gray-900">
        <Logo></Logo>
        <div>{children}</div>
      </div>
    );
  }