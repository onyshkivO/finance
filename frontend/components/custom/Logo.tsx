import { PiggyBank } from 'lucide-react'
import Link from 'next/link'
import React from 'react'

function Logo() {
  return (
    <Link href='/' className='flex item-center gap-2'>
        <PiggyBank className='stroke h-11 w-11 stroke-amber-500 
        stroke-[1.5]'/>
        <p className='bg-gradient-to-r from-amber-400 to-orange-500 
        bg-clip-text text-3xl fond-bold 
        leading-tight tracking-tighter text-transparent'>
            FinTracker
        </p>
    </Link>
  )
}
export function LogoMobile() {
    return (
        <Link href='/' className='flex item-center gap-2'>
            <p className='bg-gradient-to-r from-amber-400 to-orange-500
        bg-clip-text text-3xl fond-bold
        leading-tight tracking-tighter text-transparent'>
                FinTracker
            </p>
        </Link>
    )
}

export default Logo