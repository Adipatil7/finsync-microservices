"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { toast } from "sonner";
import Link from "next/link";
import {
  ArrowLeftRight,
  CheckCircle2,
  Users,
  Loader2,
  ArrowRight,
} from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { groupService } from "@/services/group-service";
import { settlementService } from "@/services/settlement-service";
import { formatCurrency } from "@/lib/utils";
import type { GroupResponse, SettlementSuggestion, GroupMember } from "@/types";
import { useMemo } from "react";

function GroupSettlements({ group }: { group: GroupResponse }) {
  const queryClient = useQueryClient();

  const { data: plan, isLoading } = useQuery({
    queryKey: ["settlement-plan", group.id],
    queryFn: () => settlementService.getSettlementPlan(group.id),
  });

  const { data: members } = useQuery({
    queryKey: ["group-members", group.id],
    queryFn: () => groupService.getMembers(group.id),
  });

  // Build a userId → name lookup map
  const nameMap = useMemo(() => {
    const map: Record<string, string> = {};
    if (members) {
      members.forEach((m: GroupMember) => {
        map[m.userId] = m.name || m.userId.substring(0, 8) + "...";
      });
    }
    return map;
  }, [members]);

  const getName = (userId: string) => nameMap[userId] || userId.substring(0, 8) + "...";

  const settleMutation = useMutation({
    mutationFn: (data: { payerId: string; payeeId: string; amount: number; currency: string }) =>
      settlementService.recordSettlement(group.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["settlement-plan", group.id] });
      toast.success("Settlement recorded");
    },
    onError: () => toast.error("Failed to record settlement"),
  });

  if (isLoading) {
    return (
      <div className="p-4 text-center">
        <Loader2 className="w-5 h-5 animate-spin mx-auto text-muted-foreground" />
      </div>
    );
  }

  if (!plan || plan.length === 0) {
    return (
      <div className="p-4 text-center text-muted-foreground text-sm flex items-center justify-center gap-2">
        <CheckCircle2 className="w-4 h-4 text-emerald-500" />
        All settled up
      </div>
    );
  }

  return (
    <div className="space-y-2 p-4 pt-0">
      {plan.map((suggestion, i) => (
        <div
          key={i}
          className="flex items-center justify-between p-3 rounded-lg bg-accent/30"
        >
          <div className="flex items-center gap-2 text-sm">
            <span className="font-medium">{getName(suggestion.fromUserId)}</span>
            <ArrowRight className="w-4 h-4 text-muted-foreground" />
            <span className="font-medium">{getName(suggestion.toUserId)}</span>
            <span className="font-semibold text-amber-600 ml-2">
              {formatCurrency(suggestion.amount, suggestion.currency)}
            </span>
          </div>
          <Button
            size="sm"
            variant="outline"
            className="gap-1.5 text-xs border-emerald-500/50 text-emerald-600 hover:bg-emerald-500/10"
            onClick={() =>
              settleMutation.mutate({
                payerId: suggestion.fromUserId,
                payeeId: suggestion.toUserId,
                amount: suggestion.amount,
                currency: suggestion.currency,
              })
            }
            disabled={settleMutation.isPending}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            Settle
          </Button>
        </div>
      ))}
    </div>
  );
}

export default function SettlementsPage() {
  const { data: groups, isLoading } = useQuery({
    queryKey: ["groups"],
    queryFn: () => groupService.getGroups(),
  });

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6"
    >
      <div>
        <h1 className="text-3xl font-bold">Settlements</h1>
        <p className="text-muted-foreground mt-1">View and resolve pending settlements across all groups</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      ) : groups && groups.length > 0 ? (
        <div className="space-y-4">
          {groups.map((group) => (
            <Card key={group.id}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-indigo-500/10 flex items-center justify-center">
                    <Users className="w-5 h-5 text-indigo-500" />
                  </div>
                  <div>
                    <CardTitle className="text-base">{group.name}</CardTitle>
                    <p className="text-sm text-muted-foreground">
                      {group.memberCount} members · {formatCurrency(group.totalSpent, group.currency || "INR")} total
                    </p>
                  </div>
                </div>
                <Link href={`/dashboard/groups/${group.id}`}>
                  <Button variant="ghost" size="sm" className="text-primary">
                    View Group
                  </Button>
                </Link>
              </CardHeader>
              <GroupSettlements group={group} />
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <CardContent className="py-16 text-center text-muted-foreground">
            <ArrowLeftRight className="w-12 h-12 mx-auto mb-3 opacity-50" />
            <p className="font-medium">No groups yet</p>
            <p className="text-sm">Create a group to start tracking settlements</p>
          </CardContent>
        </Card>
      )}
    </motion.div>
  );
}
