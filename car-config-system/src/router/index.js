import { ref, computed, h } from 'vue'
import Home from '../views/Home.vue'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import Configurator from '../views/Configurator.vue'
import Orders from '../views/Orders.vue'
import Cart from '../views/Cart.vue'
import About from '../views/About.vue'
import Rules from '../views/Rules.vue'
import VehicleManage from '../views/VehicleManage.vue'
import Profile from '../views/Profile.vue'
import ShemeDetail from '../views/ShemeDetail.vue'
import VehicleDetail from '../views/VehicleDetail.vue'
import VehicleList from '../views/VehicleList.vue'
import SaveScheme from '../views/SaveScheme.vue'
import PublicSchemes from '../views/PublicSchemes.vue'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/register', name: 'Register', component: Register },
  { path: '/login', name: 'Login', component: Login },
  { path: '/configure', name: 'Configurator', component: Configurator },
  { path: '/scheme-save', name: 'SaveScheme', component: SaveScheme },
  { path: '/schemes', name: 'PublicSchemes', component: PublicSchemes },
  { path: '/orders', name: 'Orders', component: Orders },
  { path: '/cart', name: 'Cart', component: Cart },
  { path: '/about', name: 'About', component: About },
  { path: '/rules', name: 'Rules', component: Rules },
  { path: '/vehicle-manage', name: 'VehicleManage', component: VehicleManage },
  { path: '/profile', name: 'Profile', component: Profile },
  { path: '/scheme-detail', name: 'SchemeDetail', component: ShemeDetail },
  { path: '/vehicle-detail', name: 'VehicleDetail', component: VehicleDetail },
  { path: '/vehicles', name: 'VehicleList', component: VehicleList }
]

function getPath() {
  const p = window.location.pathname
  return p === '' || p === '/' ? '/' : p.replace(/\/$/, '')
}

export function createWebHistory() {
  return {
    get path() { return getPath() },
    push(path) {
      const p = path.startsWith('/') ? path : '/' + path
      window.history.pushState({}, '', p)
      window.dispatchEvent(new PopStateEvent('popstate'))
    }
  }
}

export function createRouter(options) {
  const history = options.history
  const currentPath = ref(history.path)

  window.addEventListener('popstate', () => {
    currentPath.value = getPath()
  })

  const currentComponent = computed(() => {
    const r = routes.find(route => route.path === currentPath.value)
    return r ? r.component : Home
  })

  const router = {
    currentRoute: computed(() => {
      const r = routes.find(route => route.path === currentPath.value)
      return r ? { path: r.path, name: r.name } : { path: '/', name: 'Home' }
    }),
    push(path) {
      const p = path.startsWith('/') ? path : '/' + path
      currentPath.value = p
      history.push(p)
    }
  }

  const RouterView = {
    setup() {
      return () => h(currentComponent.value)
    }
  }

  const RouterLink = {
    name: 'RouterLink',
    props: { to: { type: [String, Object], required: true } },
    setup(props, { slots }) {
      const to = typeof props.to === 'string' ? props.to : props.to.path
      return () => h('a', {
        href: to,
        onClick(e) {
          e.preventDefault()
          router.push(to)
        }
      }, slots.default ? slots.default() : [])
    }
  }

  return {
    ...router,
    install(app) {
      app.provide('router', router)
      app.component('RouterView', RouterView)
      app.component('RouterLink', RouterLink)
    }
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

