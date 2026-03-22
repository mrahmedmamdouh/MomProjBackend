
/* eslint-disable no-console */
const admin = require("firebase-admin");

// ---- CLI flags ----
const argv = new Map(
  process.argv
    .slice(2)
    .map((v, i, arr) =>
      v.startsWith("--")
        ? [v.replace(/^--/, ""), arr[i + 1]?.startsWith("--") ? true : arr[i + 1] ?? true]
        : null
    )
    .filter(Boolean)
);
const APPLY = argv.has("apply");

// ---- Init ----
admin.initializeApp();
const db = admin.firestore();
const auth = admin.auth();
const now = admin.firestore.FieldValue.serverTimestamp();

function ok(...m) { console.log("✔", ...m); }
function dry() { if (!APPLY) console.log("(dry-run)"); }

async function ensureDoc(path, data) {
  const ref = db.doc(path);
  const snap = await ref.get();
  if (!snap.exists) {
    if (APPLY) await ref.set(data, { merge: true });
    ok(path, "(created)"); dry();
  } else {
    ok(path, "(exists)"); dry();
  }
}

async function setDoc(path, data) {
  if (APPLY) await db.doc(path).set(data, { merge: true });
  ok(path, "(upsert)"); dry();
}

// ──────────────────────────────────────────────────────────────
// IDs you can keep/change freely
// ──────────────────────────────────────────────────────────────
const SEL_ACME  = "seller_acme";
const SEL_HAPPY = "seller_happy";

const MOM_ALICE = "mom_alice";  // domain ID (not the auth uid)
const MOM_BETH  = "mom_beth";

const PROD_ESSENTIALS = "prod_baby_essentials";
const PROD_PRENATAL   = "prod_prenatal_yoga";
const PROD_ADVANCED   = "prod_advanced_fitness";

const SKU_ESS_BLACK  = "sku_essentials_black";
const SKU_PRENATAL_A = "sku_prenatal_batchA";
const SKU_ADVANCED_A = "sku_advanced_planA";

// ──────────────────────────────────────────────────────────────
// Seeders
// ──────────────────────────────────────────────────────────────
async function seedCategories() {
  await ensureDoc("categories/cat_mother_care", { name: "Mother Care", slug: "mother-care", createdAt: now });
  await ensureDoc("categories/cat_fitness",     { name: "Fitness", slug: "fitness", createdAt: now });
  await ensureDoc("categories/cat_nutrition",   { name: "Nutrition", slug: "nutrition", createdAt: now });
}

async function seedSellers() {
  await ensureDoc(`sellers/${SEL_ACME}`,  { name: "Acme Health",  status: "ACTIVE", createdAt: now });
  await ensureDoc(`sellers/${SEL_HAPPY}`, { name: "Happy Moms Co.", status: "ACTIVE", createdAt: now });
}

async function createFirebaseAuthUser(email, password, displayName) {
  try {
    const userRecord = await auth.createUser({
      email: email,
      password: password,
      displayName: displayName,
      emailVerified: true
    });
    ok(`Firebase Auth user created: ${userRecord.uid} (${email})`); dry();
    return userRecord.uid;
  } catch (error) {
    if (error.code === 'auth/email-already-exists') {
      const existingUser = await auth.getUserByEmail(email);
      ok(`Firebase Auth user exists: ${existingUser.uid} (${email})`); dry();
      return existingUser.uid;
    }
    throw error;
  }
}

async function seedMomsAndNids() {
  // Create Firebase Auth users first (with actual UIDs)
  const aliceUid = await createFirebaseAuthUser("alice@example.com", "password123", "Alice Mom");
  const bethUid = await createFirebaseAuthUser("beth@example.com", "password123", "Beth Mom");

  // NIDs
  await ensureDoc("nids/nid_alice", {
    imageFront: "https://example.com/nid_alice_front.jpg",
    imageBack:  "https://example.com/nid_alice_back.jpg",
    createdAt: now
  });
  await ensureDoc("nids/nid_beth", {
    imageFront: "https://example.com/nid_beth_front.jpg",
    imageBack:  "https://example.com/nid_beth_back.jpg",
    createdAt: now
  });

  await ensureDoc(`moms/${MOM_ALICE}`, {
    authUid: aliceUid,
    fullName: "Alice Mom",
    email: "alice@example.com",
    phone: "+1-555-0001",
    maritalStatus: "MARRIED",
    photoUrl: "https://example.com/alice_photo.jpg",
    numberOfSessions: 3,
    nidId: "nid_alice",
    nidRef: db.collection("nids").doc("nid_alice"),
    createdAt: now
  });

  await ensureDoc(`moms/${MOM_BETH}`, {
    authUid: bethUid,
    fullName: "Beth Mom",
    email: "beth@example.com",
    phone: "+1-555-0002",
    maritalStatus: "SINGLE",
    photoUrl: "https://example.com/beth_photo.jpg",
    numberOfSessions: 8,
    nidId: "nid_beth",
    nidRef: db.collection("nids").doc("nid_beth"),
    createdAt: now
  });

  // Optional mapping for rules helpers (momAuth/{uid} → momId)
  await ensureDoc(`momAuth/${aliceUid}`, { momId: MOM_ALICE, createdAt: now });
  await ensureDoc(`momAuth/${bethUid}`,  { momId: MOM_BETH,  createdAt: now });
}

async function seedProductsSkusOffersInventory() {
  // Products (with defaultSellerRef + minSessionsToPurchase)
  await ensureDoc(`products/${PROD_ESSENTIALS}`, {
    name: "Baby Essentials Pack",
    slug: "baby-essentials-pack",
    description: "Starter essentials.",
    status: "ACTIVE",
    defaultSellerId: SEL_ACME,
    defaultSellerRef: db.collection("sellers").doc(SEL_ACME),
    categoryIds: ["cat_mother_care"],
    minSessionsToPurchase: 0,   // open to everyone
    createdAt: now
  });

  await ensureDoc(`products/${PROD_PRENATAL}`, {
    name: "Prenatal Yoga Class",
    slug: "prenatal-yoga",
    description: "Guided prenatal yoga session.",
    status: "ACTIVE",
    defaultSellerId: SEL_HAPPY,
    defaultSellerRef: db.collection("sellers").doc(SEL_HAPPY),
    categoryIds: ["cat_fitness"],
    minSessionsToPurchase: 5,   // requires >= 5
    createdAt: now
  });

  await ensureDoc(`products/${PROD_ADVANCED}`, {
    name: "Advanced Fitness Plan",
    slug: "advanced-fitness-plan",
    description: "Advanced program.",
    status: "ACTIVE",
    defaultSellerId: SEL_HAPPY,
    defaultSellerRef: db.collection("sellers").doc(SEL_HAPPY),
    categoryIds: ["cat_fitness"],
    minSessionsToPurchase: 10,  // requires >= 10
    createdAt: now
  });

  // SKUs (top-level mirror) with productRef
  await ensureDoc(`skus/${SKU_ESS_BLACK}`, {
    productId: PROD_ESSENTIALS,
    productRef: db.collection("products").doc(PROD_ESSENTIALS),
    skuCode: "ESS-BLK",
    title: "Black",
    taxClass: "STANDARD",
    isActive: true,
    createdAt: now
  });
  await ensureDoc(`skus/${SKU_PRENATAL_A}`, {
    productId: PROD_PRENATAL,
    productRef: db.collection("products").doc(PROD_PRENATAL),
    skuCode: "PRN-A",
    title: "Batch A",
    taxClass: "SERVICE",
    isActive: true,
    createdAt: now
  });
  await ensureDoc(`skus/${SKU_ADVANCED_A}`, {
    productId: PROD_ADVANCED,
    productRef: db.collection("products").doc(PROD_ADVANCED),
    skuCode: "ADV-A",
    title: "Plan A",
    taxClass: "SERVICE",
    isActive: true,
    createdAt: now
  });

  // Optional: also create product subcollection SKUs (handy for product detail pages)
  await ensureDoc(`products/${PROD_ESSENTIALS}/skus/${SKU_ESS_BLACK}`, {
    skuCode: "ESS-BLK", title: "Black", taxClass: "STANDARD", isActive: true, createdAt: now
  });
  await ensureDoc(`products/${PROD_PRENATAL}/skus/${SKU_PRENATAL_A}`, {
    skuCode: "PRN-A", title: "Batch A", taxClass: "SERVICE", isActive: true, createdAt: now
  });
  await ensureDoc(`products/${PROD_ADVANCED}/skus/${SKU_ADVANCED_A}`, {
    skuCode: "ADV-A", title: "Plan A", taxClass: "SERVICE", isActive: true, createdAt: now
  });

  // Offers (SKU ↔ Seller) with both IDs & Refs
  await ensureDoc("skuOffers/offer_ess_acme", {
    skuId: SKU_ESS_BLACK,
    skuRef: db.collection("skus").doc(SKU_ESS_BLACK),
    sellerId: SEL_ACME,
    sellerRef: db.collection("sellers").doc(SEL_ACME),
    listPrice: 120.0,
    salePrice: 99.0,
    currency: "USD",
    isActive: true,
    activeFrom: Date.now(),
    createdAt: now
  });

  await ensureDoc("skuOffers/offer_prn_happy", {
    skuId: SKU_PRENATAL_A,
    skuRef: db.collection("skus").doc(SKU_PRENATAL_A),
    sellerId: SEL_HAPPY,
    sellerRef: db.collection("sellers").doc(SEL_HAPPY),
    listPrice: 30.0,
    salePrice: 25.0,
    currency: "USD",
    isActive: true,
    activeFrom: Date.now(),
    createdAt: now
  });

  await ensureDoc("skuOffers/offer_adv_happy", {
    skuId: SKU_ADVANCED_A,
    skuRef: db.collection("skus").doc(SKU_ADVANCED_A),
    sellerId: SEL_HAPPY,
    sellerRef: db.collection("sellers").doc(SEL_HAPPY),
    listPrice: 99.0,
    salePrice: 89.0,
    currency: "USD",
    isActive: true,
    activeFrom: Date.now(),
    createdAt: now
  });

  // Inventory
  await ensureDoc(`inventory/${SKU_ESS_BLACK}`,  { onHand: 100, reserved: 0, updatedAt: now });
  await ensureDoc(`inventory/${SKU_PRENATAL_A}`, { onHand: 25,  reserved: 0, updatedAt: now });
  await ensureDoc(`inventory/${SKU_ADVANCED_A}`, { onHand: 10,  reserved: 0, updatedAt: now });
}

async function seedCartsAndRatings() {
  // Get the actual UIDs from Firebase Auth
  const aliceUser = await auth.getUserByEmail("alice@example.com");
  const bethUser = await auth.getUserByEmail("beth@example.com");

  // Cart for Beth (eligible for Prenatal; not for Advanced)
  await ensureDoc(`carts/${MOM_BETH}`, { createdAt: now });
  await ensureDoc(`carts/${MOM_BETH}/items/${SKU_PRENATAL_A}`, {
    qty: 2,
    priceSnapshot: 25.0,
    offerId: "offer_prn_happy",
    skuRef: db.collection("skus").doc(SKU_PRENATAL_A),
    offerRef: db.collection("skuOffers").doc("offer_prn_happy"),
    addedAt: now
  });

  // Cart for Alice (only essentials)
  await ensureDoc(`carts/${MOM_ALICE}`, { createdAt: now });
  await ensureDoc(`carts/${MOM_ALICE}/items/${SKU_ESS_BLACK}`, {
    qty: 1,
    priceSnapshot: 99.0,
    offerId: "offer_ess_acme",
    skuRef: db.collection("skus").doc(SKU_ESS_BLACK),
    offerRef: db.collection("skuOffers").doc("offer_ess_acme"),
    addedAt: now
  });

  // One rating example (by Alice's uid)
  await ensureDoc(`productRatings/${PROD_ESSENTIALS}/ratings/${aliceUser.uid}`, {
    rating: 5,
    title: "Great starter",
    comment: "Loved it!",
    createdAt: now
  });
}

async function seedOrdersAndPayments() {
  // Get the actual UIDs from Firebase Auth
  const aliceUser = await auth.getUserByEmail("alice@example.com");
  const bethUser = await auth.getUserByEmail("beth@example.com");

  // --- Order for Beth (mom_beth) buying Prenatal Yoga (2x)
  const orderBethId = "order_beth_prenatal";
  const orderBethRef = db.collection("orders").doc(orderBethId);

  await setDoc(orderBethRef.path, {
    orderNo: "ORD-BETH-001",
    momId: MOM_BETH,
    momRef: db.collection("moms").doc(MOM_BETH),
    uid: bethUser.uid,
    placedAt: now,
    status: "PENDING",
    currency: "USD",
    subtotal: 50.0,
    discountTotal: 0,
    taxTotal: 0,
    shippingTotal: 0,
    grandTotal: 50.0,
  });

  await setDoc(`${orderBethRef.path}/items/1`, {
    skuId: SKU_PRENATAL_A,
    skuRef: db.collection("skus").doc(SKU_PRENATAL_A),
    productId: PROD_PRENATAL,
    productRef: db.collection("products").doc(PROD_PRENATAL),
    sellerId: SEL_HAPPY,
    sellerRef: db.collection("sellers").doc(SEL_HAPPY),
    qty: 2,
    unitPrice: 25.0,
    lineTotal: 50.0,
    productName: "Prenatal Yoga Class",
  });

  // Payment for Beth's order
  await setDoc(`payments/pay_beth_order001`, {
    orderId: orderBethId,
    orderRef: orderBethRef,
    uid: bethUser.uid,
    provider: "stripe",
    method: "card",
    amount: 50.0,
    currency: "USD",
    status: "AUTHORIZED",
    transactionRef: "txn_beth_001",
    authorizedAt: now,
  });

  // --- Order for Alice (mom_alice) buying Essentials Pack (1x)
  const orderAliceId = "order_alice_essentials";
  const orderAliceRef = db.collection("orders").doc(orderAliceId);

  await setDoc(orderAliceRef.path, {
    orderNo: "ORD-ALICE-001",
    momId: MOM_ALICE,
    momRef: db.collection("moms").doc(MOM_ALICE),
    uid: aliceUser.uid,
    placedAt: now,
    status: "PENDING",
    currency: "USD",
    subtotal: 99.0,
    discountTotal: 0,
    taxTotal: 0,
    shippingTotal: 0,
    grandTotal: 99.0,
  });

  await setDoc(`${orderAliceRef.path}/items/1`, {
    skuId: SKU_ESS_BLACK,
    skuRef: db.collection("skus").doc(SKU_ESS_BLACK),
    productId: PROD_ESSENTIALS,
    productRef: db.collection("products").doc(PROD_ESSENTIALS),
    sellerId: SEL_ACME,
    sellerRef: db.collection("sellers").doc(SEL_ACME),
    qty: 1,
    unitPrice: 99.0,
    lineTotal: 99.0,
    productName: "Baby Essentials Pack",
  });

  // Payment for Alice's order
  await setDoc(`payments/pay_alice_order001`, {
    orderId: orderAliceId,
    orderRef: orderAliceRef,
    uid: aliceUser.uid,
    provider: "paypal",
    method: "wallet",
    amount: 99.0,
    currency: "USD",
    status: "AUTHORIZED",
    transactionRef: "txn_alice_001",
    authorizedAt: now,
  });
}

/**
 * Seed doctors with Firebase Auth users and Firestore documents
 */
async function seedDoctors() {
  console.log("🩺 Seeding doctors...");

  // Doctor auth credentials
  const doctors = [
    {
      email: "dr.smith@example.com",
      password: "password123",
      fullName: "Dr. Sarah Smith",
      phone: "+1-555-0101",
      specialization: "GENERAL_MEDICINE",
      rating: 4.8,
      isAuthorized: true
    },
    {
      email: "dr.johnson@example.com", 
      password: "password123",
      fullName: "Dr. Michael Johnson",
      phone: "+1-555-0102",
      specialization: "PEDIATRICS",
      rating: 4.9,
      isAuthorized: true
    },
    {
      email: "dr.williams@example.com",
      password: "password123", 
      fullName: "Dr. Emily Williams",
      phone: "+1-555-0103",
      specialization: "GYNECOLOGY",
      rating: 4.7,
      isAuthorized: true
    },
    {
      email: "dr.brown@example.com",
      password: "password123",
      fullName: "Dr. David Brown",
      phone: "+1-555-0104", 
      specialization: "CARDIOLOGY",
      rating: 4.6,
      isAuthorized: false
    },
    {
      email: "dr.davis@example.com",
      password: "password123",
      fullName: "Dr. Jennifer Davis",
      phone: "+1-555-0105",
      specialization: "ORTHOPEDICS", 
      rating: 4.5,
      isAuthorized: true
    }
  ];

  for (const doctorData of doctors) {
    try {
      // Create Firebase Auth user
      const authUid = await createFirebaseAuthUser(doctorData.email, doctorData.password, doctorData.fullName);
      const doctorId = `doctor_${authUid}`;

      // Create doctor document in Firestore
      await ensureDoc(`doctors/${doctorId}`, {
        authUid: authUid,
        name: doctorData.fullName,
        email: doctorData.email,
        phone: doctorData.phone,
        specialization: doctorData.specialization,
        rating: doctorData.rating,
        isAuthorized: doctorData.isAuthorized,
        photo: "",
        nidId: `nid_${authUid}`,
        nidRef: `/nids/nid_${authUid}`,
        createdAt: now
      });

      // Create auth mapping for doctor
      await ensureDoc(`doctorAuth/${authUid}`, {
        doctorId: doctorId,
        createdAt: now
      });

      // Create NID document for doctor (if photos were provided)
      await ensureDoc(`nids/nid_${authUid}`, {
        doctorId: doctorId,
        doctorRef: `/doctors/${doctorId}`,
        imageFront: "",
        imageBack: "",
        createdAt: now
      });

      console.log(`✅ Doctor seeded: ${doctorData.fullName} (${doctorData.email})`);
    } catch (error) {
      console.error(`❌ Error seeding doctor ${doctorData.email}:`, error.message);
    }
  }

  console.log("🩺 Doctor seeding completed");
}


// ──────────────────────────────────────────────────────────────
// Driver
// ─────────────────────────────────────────────────────────────-
(async function main() {
  console.log(`Project: ${process.env.GOOGLE_CLOUD_PROJECT || "(default)"}  Mode: ${APPLY ? "APPLY" : "DRY-RUN"}`);

  await seedCategories();
  await seedSellers();
  await seedMomsAndNids();
  await seedDoctors();
  await seedProductsSkusOffersInventory();
  await seedCartsAndRatings();
  await seedOrdersAndPayments();

  console.log("Seeding complete.");
})().catch((e) => {
  console.error(e);
  process.exit(1);
});
