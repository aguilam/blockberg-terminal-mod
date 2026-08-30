# Blockberg Terminal

Client(This) | [Server](https://github.com/aguilam/blockberg-terminal)

This project adds features for collecting information about the trading zone on the server (e.g. SMP) and subsequently searching best offers.

## Features

- Search barrels by info on sign, and sort them by price on quantity ratio
- Search barrels by saved items in them
- Save barrels in a certain area(region)
- Autosave items in scanned barrels(Toggled in client config)
- Start local server for send and get info local
- If barrel sign text not contain formatted info, uses LLM for text parse(Enabled in server config)
- Limit zone for scanning in Server config and not accept barrels outside this area

## Commands

All commands start with **/bbt**

- **searchbarrel [item name]** search by sign text
- **searchitems [item name]** search by items in barrel
- **clearhl** clear highlighted barrels
- **setmin**
- **setmax**
- **addregion [region name]** add new region, before need set min and max region points
- **scan [region name]** scan all barrels with signs in region and send them to server
- **allregions** show all saved on client regions

