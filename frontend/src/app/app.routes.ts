import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'contacts',
    loadComponent: () => import('./components/contacts/contacts.component').then(m => m.ContactsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'leads',
    loadComponent: () => import('./components/leads/leads.component').then(m => m.LeadsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'deals',
    loadComponent: () => import('./components/deals/deals.component').then(m => m.DealsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'campaigns',
    loadComponent: () => import('./components/campaigns/campaigns.component').then(m => m.CampaignsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'reminders',
    loadComponent: () => import('./components/reminders/reminders.component').then(m => m.RemindersComponent),
    canActivate: [authGuard]
  },
  { path: '**', redirectTo: 'login' }
];
