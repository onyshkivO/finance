import React from 'react'
import {cookies} from 'next/headers';
import {redirect} from "next/navigation"
import {Button} from "@/components/ui/button";
import CreateTransactionDialog from './_components/CreateTransactionDialog';

async function page() {
    const cookieStore = await cookies();
    const userData = cookieStore.get("userData")?.value;
    const user = userData ? JSON.parse(userData) : null;
    
    if (!user) {
        
        redirect("/signin");
    }

    return (
        <div className="h-full bg-background">
            <div className="border-b bg-card">
                <div className="container mx-auto flex flex-wrap items-center justify-between gap-6 py-8">
                    <p className="text-3xl font-bold">Hello, {user.login}! </p>
                    <div className="flex items-center gap-3">
                        <CreateTransactionDialog 
                        user={user} 
                        trigger={<Button
                            variant={"outline"}
                            className="border-emerald-500 bg-emerald-950 text-white hover:bg-emerald-700 hover:text-white cursor-pointer"
                        >
                            New income
                        </Button>}
                        type="income"/>
                        <CreateTransactionDialog 
                        user={user} 
                        trigger={<Button
                            variant={"outline"}
                            className="border-rose-500 bg-rose-950 text-white hover:bg-rose-700 hover:text-white cursor-pointer"
                        >
                            New expense
                        </Button>}
                        type="expense"/>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default page