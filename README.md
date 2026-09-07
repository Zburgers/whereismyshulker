# Where Is My Shulker

This client-side Fabric mod tracks shulker boxes you place or break in a world so you never again have to wander around your world searching desparately for your valuables.

It adds a simple command to interact with the tracked data.

[Download on Modrinth](https://modrinth.com/mod/where-is-my-shulker)

---

## ✨ Features

* Tracks all placed and broken shulker boxes
* Records:
    * **Location**
    * **Custom name** (if set)
    * **Shulker box color**
* Lists all currently placed shulkers on demand
* Shulker box list supports pagination for messy evenings
* Simple command to clean the list
* Stores data in csv format

---

## 📜 Commands

### `/shulker`

Displays a list of all currently placed shulker boxes, including:

* Custom name or box color
* Coordinates
* Color

The output is paginated, if necessary.

### `/shulker clear`

Clears all undyed shulker boxes from the list.

### `/shulker clearall`

Clears *all* shulker boxes from the list.

---

## 💾 Data Storage

All data is saved as a CSV file. For *Singleplayer* worlds the path is `<world folder>/data/shulker_boxes.csv`.

On *Multiplayer* servers shulker boxes are stored in `.minecraft/.whereismyshulker/<serverip>_<port>/shulker_boxes.csv`

## 🧩 Compatibility

* Minecraft 26.2
* Fabric Loader 0.19.5 or newer
* Fabric API 0.160.0+26.2
* Java 25 for development and runtime
* Client-side only; multiplayer servers do not need this mod installed

---

## 🧱 Notes

* Only shulkers observed through this client's placement and player-break workflow are tracked. Explosions, piston movement, other-player actions, commands, and arbitrary server changes aren't tracked.
* I've searched extensively for an existing solution to this issue but without any success. I did find 9 year old reddit posts asking the same question, but since this appears to be an unsolved problem I wanted to take it into my own hands, since I regularly struggle with lost shulker boxes. 
