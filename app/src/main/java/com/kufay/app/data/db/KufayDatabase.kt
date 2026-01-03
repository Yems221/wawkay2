package com.kufay.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kufay.app.data.db.dao.NotificationDao
import com.kufay.app.data.db.entities.Notification
import javax.inject.Singleton
import java.security.SecureRandom
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import net.sqlcipher.database.SupportFactory

@Database(entities = [Notification::class], version = 5, exportSchema = false)  // ✅ CHANGÉ de 4 à 5
abstract class KufayDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        // Existing migrations
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications ADD COLUMN amountText TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notifications ADD COLUMN isIncomingTransaction INTEGER NOT NULL DEFAULT 0")

                database.execSQL(
                    "UPDATE notifications SET isIncomingTransaction = 1 " +
                            "WHERE packageName = 'com.wave.personal' AND text LIKE '%avez reçu%'"
                )

                database.execSQL(
                    "UPDATE notifications SET isIncomingTransaction = 1 " +
                            "WHERE packageName = 'com.wave.business' AND " +
                            "(text LIKE '%votre encaissement de%' OR text LIKE '%reçu%')"
                )

                database.execSQL(
                    "UPDATE notifications SET isIncomingTransaction = 1 " +
                            "WHERE packageName = 'com.google.android.apps.messaging' AND " +
                            "title LIKE '%OrangeMoney%' AND (text LIKE '%recu%' OR text LIKE '%reçu%')"
                )

                database.execSQL(
                    "UPDATE notifications SET isIncomingTransaction = 1 " +
                            "WHERE packageName = 'com.google.android.apps.messaging' AND " +
                            "title LIKE '%Mixx by Yas%' AND (text LIKE '%recu%' OR text LIKE '%reçu%')"
                )
            }
        }

        // New migration to add appTag column and label existing notifications
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the appTag column
                database.execSQL("ALTER TABLE notifications ADD COLUMN appTag TEXT")

                // Label existing notifications
                database.execSQL("""
                    UPDATE notifications 
                    SET appTag = CASE 
                        WHEN packageName = 'com.wave.personal' AND title NOT LIKE '%business%' THEN 'WAVE'
                        WHEN packageName = 'com.wave.business' THEN 'WAVE BUSINESS'
                        WHEN packageName = 'com.google.android.apps.messaging' AND LOWER(title) LIKE '%orangemoney%' THEN 'ORANGE MONEY'
                        WHEN packageName = 'com.google.android.apps.messaging' AND LOWER(title) LIKE '%mixx by yas%' THEN 'MIXX'
                        ELSE NULL
                    END
                    WHERE isIncomingTransaction = 1
                """)
            }
        }

        // ✅ NOUVELLE MIGRATION 4 -> 5 : Ajouter le champ transactionLabel
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Ajouter la colonne transactionLabel (nullable)
                database.execSQL("ALTER TABLE notifications ADD COLUMN transactionLabel TEXT")

                // ℹ️ Pas besoin de remplir les anciennes notifications
                // Le code utilise un fallback : notification.transactionLabel ?: notification.title
                // Les nouvelles notifications auront transactionLabel calculé automatiquement
            }
        }

        // Méthode pour obtenir la passphrase utilisée pour le chiffrement
        private fun getPassphrase(context: Context): ByteArray {
            val sharedPrefs = getEncryptedSharedPreferences(context)

            var passphrase = sharedPrefs.getString("db_passphrase", null)
            if (passphrase == null) {
                // Générer une nouvelle passphrase
                val random = SecureRandom()
                val passphraseBytes = ByteArray(32) // 256 bits
                random.nextBytes(passphraseBytes)
                passphrase = Base64.encodeToString(passphraseBytes, Base64.NO_WRAP)

                // Sauvegarder la passphrase
                sharedPrefs.edit().putString("db_passphrase", passphrase).apply()
            }

            return Base64.decode(passphrase, Base64.NO_WRAP)
        }

        // Méthode pour obtenir les SharedPreferences chiffrées
        private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            return EncryptedSharedPreferences.create(
                "kufay_encrypted_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        }



        @Singleton
        fun buildDatabase(context: Context): KufayDatabase {
            return Room.databaseBuilder(
                context,
                KufayDatabase::class.java,
                "kufay_database"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)  // ✅ AJOUTÉ MIGRATION_4_5
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }
    }
}
