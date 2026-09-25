package com.mrailouis.data;

import java.util.List;

public record KuudraChest(List<KuudraLootEntry> freeChest, List<KuudraLootEntry> paidChest) {
}
