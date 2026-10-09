export type HeroClassType = 'WARRIOR' | 'MAGE' | 'ARCHER' | 'ASSASSIN';

export interface HeroClassInfo {
  id: HeroClassType;
  displayName: string;
  title: string;
  role: string;
  mainAdvantage: string;
  passiveName: string;
  passiveDescription: string;
  playstyleDescription: string;
  baseHp: number;
  baseMp: number;
  basePhysicalAtk: number;
  baseMagicAtk: number;
  baseDef: number;
  baseCritRate: number; // in %
  baseSpeed: number;
  colorHex: string;
  glowHex: string;
}

export type EquipmentType = 'WEAPON' | 'ARMOR' | 'HELMET' | 'BOOTS' | 'ACCESSORY';
export type RarityType = 'COMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';

export interface EquipmentItem {
  id: string;
  name: string;
  type: EquipmentType;
  requiredClass: HeroClassType | null;
  rarity: RarityType;
  isEquipped: boolean;
  hpBonus: number;
  mpBonus: number;
  atkBonus: number;
  matkBonus: number;
  defBonus: number;
  critBonus: number;
  speedBonus: number;
  description: string;
  iconKey: string;
}

export interface HeroCharacter {
  id: string;
  userId: string;
  nickname: string;
  heroClass: HeroClassType;
  level: number;
  currentExp: number;
  maxExp: number;
  gold: number;
  diamonds: number;
  createdAt: number;
}

export interface CalculatedStats {
  hp: number;
  mp: number;
  physicalAtk: number;
  magicAtk: number;
  def: number;
  critRate: number;
  speed: number;
  combatPower: number;
  bonusHp: number;
  bonusMp: number;
  bonusAtk: number;
  bonusMatk: number;
  bonusDef: number;
  bonusCrit: number;
  bonusSpeed: number;
}

export interface UserAccount {
  username: string;
  passwordHash: string;
  activeCharacterId?: string | null;
}

export type MissionCategory = 'LOGIN' | 'TRAIN' | 'DUNGEON' | 'EQUIP' | 'CHEST';

export interface DailyMission {
  id: string;
  title: string;
  description: string;
  targetCount: number;
  currentCount: number;
  goldReward: number;
  expReward: number;
  diamondReward?: number;
  isClaimed: boolean;
  category: MissionCategory;
  iconKey: string;
}

export interface ExpPotionItem {
  id: string;
  name: string;
  tier: 'MINOR' | 'MEDIUM' | 'GREATER' | 'ANCIENT';
  expAmount: number;
  goldPrice: number;
  description: string;
  glowColor: string;
}

export interface ShopEquipmentItem extends EquipmentItem {
  goldPrice: number;
}

