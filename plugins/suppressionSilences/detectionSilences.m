clc
clear all;
close all;


signal = wavread('2.wav');
%signal = resample(signal, 5000, 32000);

% On travaille en mono
signal = signal(:,1);

figure

% Affichage du signal
subplot(311)
plot(signal, 'b')
title('Signal');


% Calcul de l'énergie (approximation)
%S = abs(signal);
%m = min(S(:));
%S = S - m;
%M = max(S(:));
%S = S/M;

%e = S.^2;
e = abs(signal).^2;

% Affichage de e
subplot(312)
plot(e, 'r')
title('~Energie~');

% Seuillage de e
seuil = .05;
e = e>seuil;% On met des 1 là où e est > à seuil, 0 sinon

% Affichage su seuil
hold on
plot([0 numel(e)],[seuil seuil],'m-')

% Détection des "silences"
idx(1) = find(e>seuil,1,'first');
idx(2) = find(e>seuil,1,'last')

signal(1:idx(1)) = 0;% on met à 0 les échantillons du début qui ne dépassés pas le seuil
signal(idx(2):end) = 0;% pareil pour la fin

% Affichage des silences détectés
subplot(313)
plot(signal, 'b')
title('Signal + filtrage');
