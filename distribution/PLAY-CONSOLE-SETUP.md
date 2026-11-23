# FlipIQ Play Console Setup — One-Time Steps

After these steps, every push to main auto-uploads to Play internal track.

---

## Step 1 — Create the app in Play Console (5 min)

1. Go to play.google.com/console
2. Create app → App name: **FlipIQ — Reseller Calculator**
3. Default language: English (US)
4. App or game: App
5. Free or paid: Free (Pro is handled by in-app billing)
6. Accept policies → Create app

---

## Step 2 — Fill the store listing

Copy from: `distribution/play-store-listing.md` in this repo.

- Title: FlipIQ — Reseller Calculator
- Short description: (from listing file)
- Full description: (from listing file)
- Category: Finance
- Contact email: haleclh@gmail.com
- Privacy policy URL: https://crtcai.com/privacy

Screenshots: Need 2–8 phone screenshots. Run the app and capture:
1. Scan screen with a BUY result (green)
2. Result screen showing profit/ROI breakdown
3. History screen with a few saved scans
4. Settings screen showing Pro upgrade

---

## Step 3 — Create service account for CI auto-upload (10 min)

This makes every build auto-upload to Play internal track.

1. In Play Console → Setup → API access
2. Click "Link to a Google Cloud project" → create new project or use existing
3. Click "Create new service account"
4. In Google Cloud Console that opens:
   - Name: flipiq-ci-upload
   - Role: Service Account User
   - Click Continue → Done
5. Back in Play Console → Grant access → Release Manager role
6. In Google Cloud Console → IAM → Service Accounts → flipiq-ci-upload
   → Keys → Add Key → JSON → download the file

---

## Step 4 — Add secret to GitHub (2 min)

1. Open the downloaded JSON file in a text editor
2. Copy ALL the contents
3. Go to github.com/Halefire84/flipiq → Settings → Secrets → Actions
4. New secret → Name: `PLAY_SERVICE_ACCOUNT_JSON` → paste JSON → Save

**That's it.** Next push to main will build, sign, and upload to Play internal track automatically.

---

## Step 5 — Set upload key in Play Console

Before the first upload works, Play Console needs to know the upload key fingerprint.

The FlipIQ keystore SHA-256 is stored in:
`~/starship-vault/credentials/flipiq/KEYS.md`

In Play Console → App signing → Upload key certificate → provide the SHA-256.

---

## Step 6 — Roll out to Production

After internal track upload succeeds:
- Play Console → Testing → Internal testing → promote to Production
- Set rollout percentage (start at 20%)
