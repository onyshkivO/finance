"use client";

import Link from "next/link";

import { useActionState } from "react";

import { loginUserAction } from "@/data/actions/auth-actions";
import { BackendErrors } from "@/components/custom/auth_errors";
import { ZodErrors } from "@/components/custom/zod-errors";
import { SubmitButton } from "@/components/custom/submit-button";

import {
  CardTitle,
  CardDescription,
  CardHeader,
  CardContent,
  CardFooter,
  Card,
} from "@/components/ui/card";

import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";

const INITIAL_STATE = {
  data: null,
};

export function SigninForm() {
    const [formState, formAction] = useActionState(loginUserAction, INITIAL_STATE);
  
    console.log("## will render on client ##");
    console.log(formState);
    console.log("###########################");

  return (
    <div className="w-full max-w-md">
      <form  action={formAction}>
        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-3xl font-bold">Sign In</CardTitle>
            <CardDescription>
              Enter your details to sign in to your account
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="login">Login</Label>
              <Input
                id="login"
                name="login"
                defaultValue={formState?.inputs?.login}
                type="text"
                placeholder="login"
              />
              <ZodErrors error={formState?.zodErrors?.login} />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                name="password"
                defaultValue={formState?.inputs?.password}
                type="password"
                placeholder="password"
              />
              <ZodErrors error={formState?.zodErrors?.password} />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col items-center justify-center">
          <BackendErrors error={formState?.authErrors} />
            <SubmitButton className="w-full" text="Sign Up" loadingText="Loading" />
          </CardFooter>
        </Card>
        <div className="mt-4 text-center text-sm">
          Don&apos;t have an account?
          <Link className="underline ml-2" href="signup">
            Sign Up
          </Link>
        </div>
      </form>
    </div>
  );
}