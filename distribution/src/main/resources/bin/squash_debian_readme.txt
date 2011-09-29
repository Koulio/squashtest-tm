# squash_debian_readme.txt : Installation de l'environnement du script de lancement de squash

1) Les livrables :
   - squash_default : Fichier d'initialisation de variables d'environnement utiles � squash.
		En �tant connect� : root
		- A placer dans le r�pertoire syst�me /etc/default.
		- A renommer en squash (mv squash_default squash).
		- Contient la variable SQUASH_HOME qui doit �tre obligatoirement initialis�e avec le chemin
		  o� sera install�e l'application : squash.
		  Ex: SQUASH_HOME=/home/johndoe/squashtest-csp-0.20.1 (au dessus de ./bin)
		- Contient �galement certaines variables (en commentaire), d�finies par d�faut dans
		  le script shell de lancement de squash, mais qui peuvent �tre surcharg�es si n�cessaire.
		  Ex: HTTP_PORT = 2603 (sans le # devant) 
			Pour surcharger la valeur 8080 d�finie par d�faut dans le script shell de lancement de squash.
		  Ex: JAVA_ARGS = "-Xmx512m"
			Pour rajouter des arguments suppl�mentaires au lancement de la JVM (seul l'argument -server est pass� par d�faut).

   - squash_debian : Script shell de lancement de squash
		A placer dans le r�pertoire $SQUASH_HOME/bin.
		A renommer en squash (mv squash_debian squash).
		Donner les droits d'ex�cution si n�cessaire au script shell : squash
		Ex: chmod +x squash

2) Les fichiers � supprimer :
	(org.apache.felix.gogo.command-0.6.1.jar, org.apache.felix.gogo.runtime-0.6.1.jar, org.apache.felix.gogo.shell-0.6.1.jar) :
		Ces fichiers permettant la gestion de la console OSGI "gogo", seront � supprimer du r�pertoire bundles/ du package de livraison de squash.

3) D�marrage automatique du service : squash
	En �tant connect� : root
	- Dans le r�pertoire /etc/init.d, cr�er un lien symbolique "squash" pointant sur l'emplacement physique
	  du script shell de lancement de squash.
	  Ex:Si SQUASH_HOME=/home/johndoe/squashtest-tm-1.0.0.RELEASE (d�finie dans /etc/default/squash)
	  ln -s /home/johndoe/squashtest-tm-1.0.0.RELEASE/bin/squash
	- Pour d�marrer le service squash au lancement de la machine :
	  update_rc.d squash defaults

4) Commande de gestion du service : squash
	En �tant connect� : root
	- /etc/init.d/squash status  : Pour consulter l'�tat du service squash.
	- /etc/init.d/squash start   : Pour d�marrer le service squash.
	- /etc/init.d/squash stop    : Pour arr�ter le service squash.
	- /etc/init.d/squash restart : Pour arr�ter puis red�marrer le service squash.
	
