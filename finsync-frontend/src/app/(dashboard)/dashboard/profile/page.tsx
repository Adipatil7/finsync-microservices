"use client";

import { useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { LogOut, Mail, User, Shield, Calendar } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { useAuthStore } from "@/lib/auth-store";

export default function ProfilePage() {
  const router = useRouter();
  const { name, email, userId, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const profileFields = [
    {
      label: "Full Name",
      value: name || "Not set",
      icon: User,
    },
    {
      label: "Email Address",
      value: email || "Not set",
      icon: Mail,
    },
    {
      label: "User ID",
      value: userId || "Not set",
      icon: Shield,
    },
  ];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-2xl"
    >
      <div>
        <h1 className="text-3xl font-bold">Profile</h1>
        <p className="text-muted-foreground mt-1">Your account information</p>
      </div>

      {/* Profile Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="w-20 h-20 rounded-2xl gradient-primary flex items-center justify-center text-white text-3xl font-bold shadow-lg shadow-indigo-500/25">
              {name?.charAt(0)?.toUpperCase() || "U"}
            </div>
            <div>
              <h2 className="text-2xl font-bold">{name || "User"}</h2>
              <p className="text-muted-foreground">{email}</p>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-1">
          <Separator className="mb-4" />
          {profileFields.map((field) => (
            <div
              key={field.label}
              className="flex items-center gap-4 p-4 rounded-lg hover:bg-accent/30 transition-colors"
            >
              <div className="w-10 h-10 rounded-lg bg-accent flex items-center justify-center">
                <field.icon className="w-5 h-5 text-muted-foreground" />
              </div>
              <div className="flex-1">
                <p className="text-sm text-muted-foreground">{field.label}</p>
                <p className="font-medium">{field.value}</p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Danger Zone */}
      <Card className="border-destructive/20">
        <CardHeader>
          <CardTitle className="text-lg text-destructive">Session</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground mb-4">
            Sign out of your account. You will need to log in again to access the dashboard.
          </p>
          <Button variant="destructive" onClick={handleLogout} className="gap-2">
            <LogOut className="w-4 h-4" />
            Sign Out
          </Button>
        </CardContent>
      </Card>
    </motion.div>
  );
}
