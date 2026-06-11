# IR-SellChest

IR-SellChest è un fork di AutoSellChests adattato per funzionare con IR-Shop al posto di EconomyShopGUI.

## Requisiti

- Java 21
- PaperMC 1.21+
- Vault
- IR-Shop

## Dipendenze opzionali

- DecentHolograms / FancyHolograms — per gli ologrammi sulle sell chest
- Essentials / CMI — per il rilevamento AFK

## Installazione

1. Compila il plugin con Gradle.
2. Copia il file `build/libs/*.jar` nella cartella `plugins/` del tuo server.
3. Assicurati che Vault e IR-Shop siano installati.
4. Avvia o riavvia il server.

## Costruzione

```bash
gradlew.bat build
```

Output: `build/libs/IR-SellChest-<version>.jar`

## Differenze rispetto all'originale

- Rimossa dipendenza da EconomyShopGUI — usa IR-Shop + Vault
- Sistema multi-economia semplificato a singolo double (Vault)
- Sell chest già piazzate nel mondo restano compatibili (backward compat sul DB)
- Cache degli item vendibili di IR-Shop per performance ottimali

## Dettagli

- Nome plugin: `AutoSellChests`
- Main class: `me.gypopo.autosellchests.AutoSellChests`
- API Minecraft: `1.19`
- Repository: https://github.com/ItaliaRevengee/IR-SellChest
